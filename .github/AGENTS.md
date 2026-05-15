# Agent Instructions
-Đọc ARCHITECTURE.md trước khi viết hoặc sửa bất kỳ file Java nào.
-"The terminal environment is PowerShell (pwsh). You MUST use PowerShell-compatible syntax for all terminal commands. For creating multiple directories, use comma-separated strings like mkdir "dir1", "dir2" or chain them using semicolons like mkdir dir1; mkdir dir2."
Additional explanation:
In PowerShell, to create multiple directories simultaneously, the correct syntax that the AI must generate is one of the following two methods:

-Using an array (comma-separated): mkdir src\...\controller, src\...\service, src\...\repository

-Separating into individual commands (semicolon-separated): mkdir src\...\controller; mkdir src\...\service; ...