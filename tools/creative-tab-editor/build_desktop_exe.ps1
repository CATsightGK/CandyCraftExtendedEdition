$ErrorActionPreference = "Stop"

$editorDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$desktop = [Environment]::GetFolderPath("Desktop")
$output = Join-Path $desktop "CandyCraft-Creative-Inventory-Editor.exe"
$stage = Join-Path $env:TEMP "CandyCraft-Creative-Inventory-Editor-stage"
$node = (Get-Command node -ErrorAction Stop).Source
$csc = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if (-not (Test-Path $csc)) {
  throw "Microsoft C# compiler was not found"
}

Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $stage | Out-Null
Copy-Item -LiteralPath $node -Destination (Join-Path $stage "node.exe")
Copy-Item -LiteralPath (Join-Path $editorDir "server.js") -Destination $stage
Copy-Item -LiteralPath (Join-Path $editorDir "index.html") -Destination $stage
Copy-Item -LiteralPath (Join-Path $editorDir "style.css") -Destination $stage

try {
  & $csc /nologo /target:winexe /optimize+ /out:$output `
    /resource:"$(Join-Path $stage 'node.exe'),node.exe" `
    /resource:"$(Join-Path $stage 'server.js'),server.js" `
    /resource:"$(Join-Path $stage 'index.html'),index.html" `
    /resource:"$(Join-Path $stage 'style.css'),style.css" `
    (Join-Path $editorDir "desktop_launcher.cs")
  if ($LASTEXITCODE -ne 0 -or -not (Test-Path $output)) {
    throw "EXE packaging failed with exit code $LASTEXITCODE"
  }
} finally {
  Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host "Created $output"
