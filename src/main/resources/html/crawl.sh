for i in {1..252}; do
  wget "http://www.jo.se/sv/JO-beslut/Soka-JO-beslut/?searchType=decision&query=*&pn=${i}" &
done
