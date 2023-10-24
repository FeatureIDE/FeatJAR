:<<BATCH
    @echo off
for /F "tokens=*" %%A in (scripts/repo.txt) do git clone --branch evaluation_interaction_analysis2 %%A
    exit /b
BATCH
xargs -L1 git clone --progress --branch evaluation_interaction_analysis2 < scripts/repo.txt