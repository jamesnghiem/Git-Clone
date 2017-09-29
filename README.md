# Git-Clone

Git-Clone is an offline version-control system with many git commands built in such as commit, checkout, and merge. Dependencies and modules the project uses have been included in the lib folder.

File names are converted into SHA-hash ids and file information is stored as byte arrays which are then stored in its respective Commit (CommitNode.java). Each Commit is connected to its respective branch (BranchNode.java) and is tracked by a unique SHA-hash id based on the contents of the files it contains. Before a commit is officially pushed, the files for that commit are contained in the staging area (Staging.java), which keeps track of modified files, added files, deleted files, etc.

Note that Branch.java and Commit.java serve to encapsulate our branch and commit nodes, which are structurally similar to trees.

FileIO contains functions that serialize/deserialize input files. Repo.java is where all the action happens (code for git commands, helper methods, etc, etc)..


I created this project with Edward Sa, Leon Kwak, Dahyun Kim, and Jiwhan Bae in Summer 2017 as part of a Data Structures course at UC Berkeley.
