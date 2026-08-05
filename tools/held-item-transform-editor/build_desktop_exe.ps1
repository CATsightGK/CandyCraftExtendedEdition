$ErrorActionPreference = "Stop"

$editorDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$desktop = [Environment]::GetFolderPath("Desktop")
$output = Join-Path $desktop "CandyCraft-Held-Item-Transform-Editor.exe"
$stage = Join-Path $env:TEMP "CandyCraft-Held-Item-Transform-Editor-stage"
$node = (Get-Command node -ErrorAction Stop).Source
$csc = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if (-not (Test-Path $csc)) { throw "Microsoft C# compiler was not found" }

Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $stage | Out-Null
Copy-Item -LiteralPath $node -Destination (Join-Path $stage "node.exe")
foreach ($name in @("server.js", "index.html", "style.css", "app.js")) {
  Copy-Item -LiteralPath (Join-Path $editorDir $name) -Destination $stage
}
Copy-Item -LiteralPath (Join-Path $editorDir "vendor") -Destination $stage -Recurse
Copy-Item -LiteralPath (Join-Path $editorDir "assets") -Destination $stage -Recurse

try {
  $arguments = @("/nologo", "/target:winexe", "/optimize+", "/out:$output")
  foreach ($file in Get-ChildItem -LiteralPath $stage -Recurse -File) {
    $logicalName = $file.FullName.Substring($stage.Length + 1).Replace("\", "/")
    $arguments += "/resource:$($file.FullName),$logicalName"
  }
  $arguments += (Join-Path $editorDir "desktop_launcher.cs")
  & $csc @arguments
  if ($LASTEXITCODE -ne 0 -or -not (Test-Path $output)) { throw "EXE packaging failed with exit code $LASTEXITCODE" }
} finally {
  Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host "Created $output"
