param(
    [Parameter(Mandatory = $true)]
    [string]$FreeRdpRoot
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$target = (Resolve-Path -LiteralPath $FreeRdpRoot).Path

if (-not (Test-Path -LiteralPath (Join-Path $target '.git'))) {
    throw "Not a FreeRDP Git checkout: $target"
}

$expected = '69afd13014b3a5f5e32d649112ce6f06d8024449'
$actual = (& git -C $target rev-parse HEAD).Trim()
if ($actual -ne $expected) {
    throw "FreeRDP commit mismatch. Expected $expected, got $actual"
}

& git -C $target apply (Join-Path $repoRoot 'patches\cc10-android.patch')
if ($LASTEXITCODE -ne 0) { throw 'Unable to apply CC10 patch.' }

Copy-Item -Path (Join-Path $repoRoot 'overlay\*') -Destination $target -Recurse -Force
Write-Host 'CC10 Remote Desktop sources applied successfully.'
