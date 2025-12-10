$javaFiles = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse
Write-Host "Found $($javaFiles.Count) Java files. Removing comments..." -ForegroundColor Cyan

foreach ($file in $javaFiles) {
    $lines = Get-Content $file.FullName
    $newLines = @()
    $inBlockComment = $false
    
    foreach ($line in $lines) {
        if ($line -match '^\s*/\*') {
            $inBlockComment = $true
            if ($line -match '\*/\s*$') {
                $inBlockComment = $false
            }
            continue
        }
        
        if ($inBlockComment) {
            if ($line -match '\*/') {
                $inBlockComment = $false
            }
            continue
        }
        
        if ($line -match '^\s*//') {
            continue
        }
        
        $cleanLine = $line -replace '//.*$', ''
        $newLines += $cleanLine
    }
    
    Set-Content $file.FullName -Value $newLines
    Write-Host "✓ Processed: $($file.Name)" -ForegroundColor Green
}

Write-Host "`nCompleted! All comments removed from $($javaFiles.Count) Java files." -ForegroundColor Cyan
