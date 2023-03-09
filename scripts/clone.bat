:<<BATCH
    @echo off
for /F "tokens=*" %%A in (scripts/repo.txt) do git clone --branch evaluation_interaction_analysis %%A
    exit /b
BATCH
xargs -L1 git clone --progress --branch evaluation_interaction_analysis < scripts/repo.txt
