# run_calibracion.ps1
Set-Location $PSScriptRoot
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host ">>> Compilando y empaquetando..." -ForegroundColor Cyan
mvn -q clean package
if ($LASTEXITCODE -ne 0) { throw "Fallo mvn package" }

$jar = "target\materiales-calibracion-jar-with-dependencies.jar"
if (!(Test-Path $jar)) { throw "No existe el jar: $jar" }

# calibracion para:
$instancias = @("mediana")   # mediana
$configs    = 0..26          # C0..C26
$runs       = 5              # 5 runs

foreach ($inst in $instancias) {
  foreach ($cfgNum in $configs) {
    $cfg = "C$cfgNum"
    for ($r = 0; $r -lt $runs; $r++) {

      Write-Host ">>> Ejecutando instancia=$inst config=$cfg run=$r" -ForegroundColor Yellow

      java -jar $jar $inst $cfg $r

      if ($LASTEXITCODE -ne 0) {
        Write-Host "!!! Fallo: instancia=$inst config=$cfg run=$r" -ForegroundColor Red
        exit 1
      }
    }
  }
}

Write-Host ">>> CALIBRACION FINALIZADA" -ForegroundColor Green
