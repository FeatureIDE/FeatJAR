:<<BATCH
    @echo off
for /F "tokens=*" %%A in (scripts/repo.txt) do git clone --branch main %%A
    exit /b
BATCH
xargs -L1 git clone --progress --branch main < scripts/repo.txt