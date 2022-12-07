:<<BATCH
    @echo off
    for /F "tokens=*" %%A in (scripts/repo.txt) do git clone --branch v0.1.0 %%A
    exit /b
BATCH
xargs -L1 git clone --progress --branch v0.1.0 < scripts/repo.txt
