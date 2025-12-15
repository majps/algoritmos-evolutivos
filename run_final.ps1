# run_final.ps1
Set-Location $PSScriptRoot

Write-Host ">>> Compilando y empaquetando..." -ForegroundColor Cyan
mvn -q clean package
if ($LASTEXITCODE -ne 0) { throw "Fallo mvn package" }

$jar = "target/materiales-final-jar-with-dependencies.jar"
if (!(Test-Path $jar)) { throw "No existe el jar: $jar" }

$instancias = @("pequena", "mediana", "grande")
$runs = 10

foreach ($inst in $instancias) {
  for ($r = 0; $r -lt $runs; $r++) {
    Write-Host ">>> Ejecutando instancia=$inst run=$r" -ForegroundColor Yellow
    java -jar $jar $inst $r

    if ($LASTEXITCODE -ne 0) {
      Write-Host "!!! Fallo: instancia=$inst run=$r" -ForegroundColor Red
      exit 1
    }
  }
}

Write-Host ">>> FINALIZADO" -ForegroundColor Green
