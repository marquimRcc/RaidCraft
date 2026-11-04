# Local Paper 1.21.11 lab. Use -AcceptEula only after reading the Minecraft EULA.
[CmdletBinding()]
param(
    [string]$Root = (Join-Path $HOME 'RaidCraft-Lab'),
    [switch]$AcceptEula,
    [switch]$Start
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

foreach ($command in @('java', 'javac')) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "Required Java 21 JDK command not found: $command. Install https://adoptium.net/temurin/releases/?version=21"
    }
}
$javaVersion = (& java --version | Out-String)
$javacVersion = (& javac -version 2>&1 | Out-String)
if ($javaVersion -notmatch 'version "21(\.|")|(^|\n)(openjdk|java) 21(\.|\s)' -or
    $javacVersion -notmatch 'javac 21(\.|(\r?\n)|$)') {
    throw "Java 21 JDK is required. Detected: $javaVersion $javacVersion"
}

$downloads = Join-Path $Root 'downloads'
$sourceRoot = Join-Path $Root 'source'
$toolsRoot = Join-Path $Root 'tools'
$serverRoot = Join-Path $Root 'server'
$plugins = Join-Path $serverRoot 'plugins'
foreach ($folder in @($downloads, $sourceRoot, $toolsRoot, $plugins)) {
    New-Item -ItemType Directory -Path $folder -Force | Out-Null
}
$sourceZip = Join-Path $downloads 'RaidCraft-main.zip'
Write-Host '[1/4] Downloading public RaidCraft source...'
Invoke-WebRequest -Uri 'https://github.com/marquimRcc/RaidCraft/archive/refs/heads/main.zip' -OutFile $sourceZip -UseBasicParsing
Expand-Archive -Path $sourceZip -DestinationPath $sourceRoot -Force
$source = Join-Path $sourceRoot 'RaidCraft-main'
if (-not (Test-Path (Join-Path $source 'build.gradle.kts'))) {
    throw 'RaidCraft source was not extracted correctly.'
}

$gradlePath = $null
$gradleCommand = Get-Command 'gradle' -ErrorAction SilentlyContinue
if ($gradleCommand) {
    $versionText = (& gradle --version | Out-String)
    if ($versionText -match 'Gradle 8\.14') { $gradlePath = $gradleCommand.Source }
}
if (-not $gradlePath) {
    Write-Host '[2/4] Getting portable Gradle 8.14...'
    $gradleZip = Join-Path $downloads 'gradle-8.14-bin.zip'
    $gradleUrl = 'https://services.gradle.org/distributions/gradle-8.14-bin.zip'
    $gradleChecksum = ([string](Invoke-RestMethod -Uri ($gradleUrl + '.sha256'))).Trim().ToLowerInvariant()
    $downloadGradle = $true
    if (Test-Path $gradleZip) {
        $downloadGradle = ((Get-FileHash -Path $gradleZip -Algorithm SHA256).Hash.ToLowerInvariant() -ne $gradleChecksum)
    }
    if ($downloadGradle) { Invoke-WebRequest -Uri $gradleUrl -OutFile $gradleZip -UseBasicParsing }
    if ((Get-FileHash -Path $gradleZip -Algorithm SHA256).Hash.ToLowerInvariant() -ne $gradleChecksum) {
        throw 'Portable Gradle checksum verification failed.'
    }
    Expand-Archive -Path $gradleZip -DestinationPath $toolsRoot -Force
    $gradlePath = Join-Path $toolsRoot 'gradle-8.14\bin\gradle.bat'
}

Write-Host '[2/4] Compiling the plugin...'
& $gradlePath --project-dir $source --no-daemon clean build
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed with exit code $LASTEXITCODE" }
$pluginJar = Join-Path $source 'build\libs\RaidCraft-0.1.0.jar'
if (-not (Test-Path $pluginJar)) { throw "RaidCraft JAR was not created: $pluginJar" }
Copy-Item $pluginJar (Join-Path $plugins 'RaidCraft-0.1.0.jar') -Force

Write-Host '[3/4] Downloading verified Paper 1.21.11...'
$headers = @{ 'User-Agent' = 'RaidCraft-Local-Lab/0.1 (https://github.com/marquimRcc/RaidCraft)' }
$builds = @(Invoke-RestMethod -Uri 'https://fill.papermc.io/v3/projects/paper/versions/1.21.11/builds' -Headers $headers)
$stable = $builds | Where-Object { $_.channel -eq 'STABLE' -and $_.downloads.'server:default'.url } | Select-Object -First 1
if (-not $stable) { throw 'No stable Paper 1.21.11 build found.' }
$paperInfo = $stable.downloads.'server:default'
$paperChecksum = ([string]$paperInfo.checksums.sha256).ToLowerInvariant()
$paperJar = Join-Path $serverRoot 'paper.jar'
$downloadPaper = $true
if (Test-Path $paperJar) {
    $downloadPaper = ((Get-FileHash -Path $paperJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $paperChecksum)
}
if ($downloadPaper) {
    $temporaryJar = $paperJar + '.tmp'
    Invoke-WebRequest -Uri $paperInfo.url -Headers $headers -OutFile $temporaryJar -UseBasicParsing
    if ((Get-FileHash -Path $temporaryJar -Algorithm SHA256).Hash.ToLowerInvariant() -ne $paperChecksum) {
        Remove-Item $temporaryJar -Force
        throw 'Paper checksum verification failed.'
    }
    Move-Item -Path $temporaryJar -Destination $paperJar -Force
}

$propertiesPath = Join-Path $serverRoot 'server.properties'
if (-not (Test-Path $propertiesPath)) {
@'
server-ip=127.0.0.1
server-port=25565
online-mode=true
max-players=2
view-distance=4
simulation-distance=4
spawn-protection=0
motd=RaidCraft local test server
'@ | Set-Content -Path $propertiesPath -Encoding ASCII
}

$eulaPath = Join-Path $serverRoot 'eula.txt'
if ($AcceptEula) {
    'eula=true' | Set-Content -Path $eulaPath -Encoding ASCII
} elseif (-not (Test-Path $eulaPath)) {
    'eula=false' | Set-Content -Path $eulaPath -Encoding ASCII
}

Write-Host "[4/4] Lab ready at: $serverRoot"
Write-Host 'Minecraft Java Edition 1.21.11: connect to 127.0.0.1'
Write-Host 'Paper console: op YOUR_MINECRAFT_NICK'
Write-Host 'In Minecraft, look at stone and enter: /rc create diamond01 DIAMOND 2 30'

if ($Start) {
    if (-not ((Get-Content $eulaPath -Raw) -match '(?m)^eula=true\s*$')) {
        throw 'EULA not accepted. Read https://aka.ms/MinecraftEULA and run with -AcceptEula -Start.'
    }
    Write-Host 'Starting Paper. Press Ctrl+C to stop.'
    Push-Location $serverRoot
    try {
        & java '-Xms512M' '-Xmx2G' '-jar' 'paper.jar' '--nogui'
        if ($LASTEXITCODE -ne 0) { throw "Paper exited with code $LASTEXITCODE" }
    } finally {
        Pop-Location
    }
} else {
    Write-Host 'To start later, rerun this script with -AcceptEula -Start.'
}
