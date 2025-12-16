#!/bin/bash

CSV_FILE="NecessaryFilesAndData/ProposalsFromCitizens/VotesData.csv"
POSITION=$1


LINE=$(( POSITION / 6 + 1 ))
COLUMN=$(( POSITION % 6 + 1 ))


VALUE=$(head -n "$LINE" "$CSV_FILE" | tail -n 1 | cut -d',' -f"$COLUMN")
echo "$VALUE"
