:<<BATCH
    @echo off
    for /F "tokens=*" %%A in (scripts/repo.txt) do git clone --branch evaluation_continuous_sampling %%A
    exit /b
BATCH
xargs -L1 git clone --progress --branch evaluation_continuous_sampling < scripts/repo.txt
