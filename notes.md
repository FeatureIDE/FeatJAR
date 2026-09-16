regular expression: "\\s*((endif\\s*)|(else\\s*)|(if\\s+(.+))|(elif\\s+(.+)))"

Group 1: the whole choice of annotations
  Group 2: (endif\s*)
  Group 3: (else\s*)
  Group 4: (if\s+(.+))
    Group 5: (.+) — the condition after if
  Group 6: (elif\s+(.+))
    Group 7: (.+) — the condition after elif