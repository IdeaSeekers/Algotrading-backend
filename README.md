# Algotrading-backend
The backend part of Algotrading

## How to install ELK


1. При условии, что у вас установлен докер.

```shell
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.5.3
docker pull docker.elastic.co/kibana/kibana:8.5.3
docker network create elastic
docker run -d --name elasticsearch --net elastic -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.5.3
docker run -d --name kibana --net elastic -p 5601:5601 -e "ELASTICSEARCH_HOSTS=https://elasticsearch:9200" docker.elastic.co/kibana/kibana:8.5.3
```

2. Убедитесь, что http://localhost:5601/ выдаёт веб-морду, а http://localhost:9200/

```json
{
  "name" : "460541ec022b",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "N14eu9FPTziKJF5CmmFC1A",
  "version" : {
    "number" : "8.5.2",
    "build_flavor" : "default",
    "build_type" : "docker",
    "build_hash" : "a846182fa16b4ebfcc89aa3c11a11fd5adf3de04",
    "build_date" : "2022-11-17T18:56:17.538630285Z",
    "build_snapshot" : false,
    "lucene_version" : "9.4.1",
    "minimum_wire_compatibility_version" : "7.17.0",
    "minimum_index_compatibility_version" : "7.0.0"
  },
  "tagline" : "You Know, for Search"
}
```


2. Вам в любом случае нужно скачать [LogStash](https://www.elastic.co/downloads/logstash) локально

   - Разархивируйте архив куда-нибудь, например, в `C:\tools\ `
    
   - Запустите с правильными путями, перед этим отредачьте `logstash.conf`
```bash
   C:\tools\logstash-8.5.2\bin\logstash.bat -f C:\Users\Sergey\study\term-7\software-engineering\algotrading\logstash.conf
```

3. Запустите сервер, потыкайтесь в запросы немного

4. - Зайдите на http://localhost:5601/, должна открыться веб-страничка
   - Идём в http://localhost:5601/app/management/kibana/dataViews
   - В Index pattern пишем `server-api*`
   - Идём в http://localhost:5601/app/dashboards и создаём нужные графички

5. Победа!

PS:
Чтобы поддержать новые логи, нужно просто залоггировать всё аналогично рутам и добавить графички.
