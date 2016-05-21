for f in $(find . -name "index.html*"); do
  pdf_links=$(cat $f | pup "a attr{href}" | grep PageFiles)
  for suffix in $pdf_links; do
    link="http://www.jo.se${suffix}"
    filename=$(echo $suffix | sed 's/.*\///')
    if [ ! -f $filename ]; then
      wget "$link"
    fi
  done
done
