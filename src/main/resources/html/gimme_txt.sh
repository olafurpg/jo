#!/usr/bin/env bash
for f in *.pdf; do
  pdftotext -enc UTF-8 "$f"
done
