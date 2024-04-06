:<<BATCH
    @echo off
for /F "tokens=*" %%A in (scripts/repo.txt) do git clone --branch evaluation_sample_reducer %%A
    exit /b
BATCH
xargs -L1 git clone --progress --branch evaluation_sample_reducer < scripts/repo.txt
