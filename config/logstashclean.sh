#/bin/bash
echo -e "____________________________  Usage __________________________"
echo -e "To use this script you need to pass 3 arguments:  host, number of days after which records have to be cleaned, condition of deletion."
echo -e "If condition (3rd parameter) is ommited, all old records will be deleted!"
echo -e "Examples:"
echo -e "	logstashclean.sh 192.168.1.10 5 level:DEBUG 	//deletes DEBUG records older then 5 days"
echo -e "	logstashclean.sh 192.168.1.10 7 		//deletes all records older then 7 days"
echo -e "______________________________________________________________\n"
cnt=0

echo -e "========= Indexes before cleaning ==============="
curl http://$1:9200/_cat/indices?v
echo -e "================================================="
echo -e "Cleaning ElasticSearch indices older than $2 days started\n"
for i in `curl http://$1:9200/_cat/indices?v 2>/dev/null | tr ' ' '\n'|grep log` ; do
   #echo ${i}
   NOW=(`date --date="$2 days ago" +"%Y.%m.%d"`)
   latestfile="logstash-$NOW"
   if [ "$i" \< "$latestfile" ] ; then
      ((cnt++))
      echo "$i older then  $NOW, cleaning"
      if [ "$3" == "" ] ; then
         url=(`echo "http://$1:9200/${i}?pretty"`)
      else
         url=(`echo "http://$1:9200/${i}/_query?q=$3"`)
      fi
      echo -e -n "$url -> "
      curl -XDELETE $url
      echo "."
   fi
done
echo -e "========= Indexes after cleaning ==============="
curl http://$1:9200/_cat/indices?v
echo -e "================================================="

echo -e "Purging space\n"
url=(`echo "http://$1:9200/_all/_optimize?only_expunge_deletes=true"`)
echo -e -n "$url -> "
curl -XPOST $url
echo "."

echo -e "========= Indexes after purging ==============="
curl http://$1:9200/_cat/indices?v
echo -e "================================================="

echo "Cleaning done. $cnt indices were cleaned"
