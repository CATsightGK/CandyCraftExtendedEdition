$ErrorActionPreference = "Stop"
$editorDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$desktop = [Environment]::GetFolderPath("Desktop")
$output = Join-Path $desktop "CandyCraft-Recipe-Editor.exe"
$stage = Join-Path $env:TEMP "CandyCraft-Recipe-Editor-stage"
$node = (Get-Command node -ErrorAction Stop).Source
$csc = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if (-not (Test-Path $csc)) { throw "Microsoft C# compiler was not found" }

Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $stage | Out-Null
Copy-Item -LiteralPath $node -Destination (Join-Path $stage "node.exe")
foreach ($name in "server.js", "index.html", "style.css") { Copy-Item -LiteralPath (Join-Path $editorDir $name) -Destination $stage }
try {
  & $csc /nologo /target:winexe /optimize+ /out:$output `
    /resource:"$(Join-Path $stage 'node.exe'),node.exe" `
    /resource:"$(Join-Path $stage 'server.js'),server.js" `
    /resource:"$(Join-Path $stage 'index.html'),index.html" `
    /resource:"$(Join-Path $stage 'style.css'),style.css" `
    (Join-Path $editorDir "desktop_launcher.cs")
  if ($LASTEXITCODE -ne 0 -or -not (Test-Path $output)) { throw "EXE packaging failed" }
} finally { Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue }
Write-Host "Created $output"
