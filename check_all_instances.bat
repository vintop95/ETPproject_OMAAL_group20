for /l %%n in (1, 1, 7) do (
etpchecker.exe instance0%%n -check instance0%%n_OMAAL_group20
)

pause