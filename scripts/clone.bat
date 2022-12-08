:<<BATCH
    @echo off
    for /F "tokens=*" %%A in (scripts/repo.txt) do git clone --branch stable-0.1.0 %%A
    exit /b
BATCH
xargs -L1 git clone --progress --branch stable-0.1.0 < scripts/repo.txt
