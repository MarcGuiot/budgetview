BEGIN{
  "date +%Y" | getline year
  "date +%s " | getline now
  start = 0
}
{
		if (start == 0 && ($3 == year || $3 == year - 1)) {
      "date -d \"" $1 " " $2 " " $3 "\" +%s" | getline actual
		  if (now - actual < period) {
				start=1; 
			}
		}
		if (start == 1)
     {
				print $0
     }
}
