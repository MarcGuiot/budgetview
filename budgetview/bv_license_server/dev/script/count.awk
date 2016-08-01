{
  if ($2 == "ok"){
   if (ok[$4] != 1){
     countOk++
     ok[$4] = 1
   }
  }
  else if ($2 == "computeAnonymous"){
   if (anonymous[$3] != 1){
     anonymous[$3] = 1
     countAnonymous++;
   }
}
}
END{
  print "Anonymous : ", countAnonymous
  for (i in ok){
    name=i
    sub("@.*$", "", name)
    print name
  }
}
