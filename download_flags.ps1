# Script para baixar as bandeiras localmente a partir do JSON
$jsonPath = "countries.json"
$targetDir = "src/main/resources/static/images/flags"

if (!(Test-Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir -Force
}

if (!(Test-Path $jsonPath)) {
    Write-Error "Arquivo countries.json não encontrado!"
    exit
}

# Ler e parsear o JSON
$jsonContent = Get-Content $jsonPath -Raw | ConvertFrom-Json
$codes = $jsonContent.features.properties.'ISO3166-1-Alpha-2' | Select-Object -Unique

foreach ($code in $codes) {
    if ($code -and $code.Length -eq 2) {
        $code = $code.ToLower()
        $url = "https://flagcdn.com/w80/$code.png"
        $dest = "$targetDir/$code.png"
        
        if (!(Test-Path $dest)) {
            Write-Host "Baixando bandeira: $code" -ForegroundColor Cyan
            try {
                Invoke-WebRequest -Uri $url -OutFile $dest -TimeoutSec 15 -ErrorAction Stop
            } catch {
                Write-Warning "Falha ao baixar $code"
            }
        }
    }
}
Write-Host "Download concluído com sucesso!" -ForegroundColor Green
