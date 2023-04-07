# Kafka connect in Kubernetes

## Deploy Azure resources using [Terraform](https://www.terraform.io/) (version >= 0.15 should be installed on your system)
```
terraform init
terraform apply
```

## Create a custom docker image

For running the azure connector, you can create your own docker image. Create your azure connector image and build it.

- ```docker build -t <your-repo>/<your-tag> -f Dockerfile .```
  ![image](screenshots/1%20docker%20build.PNG)
- ```docker push <your-repo>/<your-tag>```
  ![image](screenshots/2%20docker%20push.PNG)
- update `confluent-platform.yaml`
  ```
  my-azure-connector:1.0.0 --> bobriashovm2/homework:10.0.3
  ```

## Launch Confluent for Kubernetes

### Set up connection to cluster
- Login in Azure CLI:
  ```
  az login
  ```

- Get credentials for your cluster:
  ```
  az aks get-credentials -g rg-kafkaconhm-westeurope -n aks-kafkaconhm-westeurope
  ```

### Create a namespace

- Create the namespace to use:

  ```cmd
  kubectl create namespace confluent
  ```

- Set this namespace to default for your Kubernetes context:

  ```cmd
  kubectl config set-context --current --namespace confluent
  ```

### Install Confluent for Kubernetes

- Add the Confluent for Kubernetes Helm repository:

  ```cmd
  helm repo add confluentinc https://packages.confluent.io/helm
  helm repo update
  ```

- Install Confluent for Kubernetes:

  ```cmd
  helm upgrade --install confluent-operator confluentinc/confluent-for-kubernetes
  ```

### Install Confluent Platform

- Install all Confluent Platform components:

  ```cmd
  kubectl apply -f ./confluent-platform.yaml
  ```
  ![image](screenshots/3%20apply%20confluence.PNG)

- Install a sample producer app and topic:

  ```cmd
  kubectl apply -f ./producer-app-data.yaml
  ```
  ![image](screenshots/4%20apply%20app%20data.PNG)

- Check that everything is deployed:

  ```cmd
  kubectl get pods -o wide 
  ```
  ![image](screenshots/5%20get%20pods.PNG)

### View Control Center

- Set up port forwarding to Control Center web UI from local machine:

  ```cmd
  kubectl port-forward controlcenter-0 9021:9021
  ```

- Browse to Control Center: [http://localhost:9021](http://localhost:9021)
  ![image](screenshots/6%20control%20center.PNG)

## Create a kafka topics

- The topic should have at least 3 partitions because the azure blob storage has 3 partitions. Name the new topic: "expedia".
  ![image](screenshots/7%20create%20expedia%20topic.PNG)
- Create topic `expedia-ext` for output enriched data in the same manner

## Prepare the azure connector configuration

Change the `azure-source-cc-expedia.json` file, update by your Azure Storage credentials:
- `azblob.account.name`
- `azblob.account.key`
- `azblob.container.name`

## Upload the connector file through the API

Since Kafka Connect is intended to be run as a service, it also supports a REST API for managing connectors. By default this service runs on port 8083.
- Set up port forwarding to Connect pod from your local machine:
  ```cmd
  kubectl port-forward connect-0 8083:8083
  ```

- Upload the connector using following API:
  ```cmd
  curl -X POST -H "Content-Type: application/json" -d @/connectors/azure-source-cc-expedia.json http://localhost:8083/connectors
  ```

- Check connector in Confluence Center:
  ![image](screenshots/8%20connector%20created.PNG)
  
- Check data in `expedia` topic:
  ![image](screenshots/9%20expedia%20topic%20data.PNG)

## Implement you KStream application

- Add necessary code and configuration to [KStream Application Class](src/main/java/com/epam/bd201/KStreamsApplication.java)

- Build KStream application jar
  ```cmd
  $ mvn package
  ```
  ![image](screenshots/10%20mvn%20build%20app.PNG)

- Build [KStream Docker Image](Dockerfile) - insert valid Azure image registry here
  ```cmd
  $ docker build -t image-registry/your-project-id/kstream-app:1.0
  ```
  ![image](screenshots/11%20docker%20build%20app.PNG)

- Push KStream image to Container Registry
  ```cmd
  $ docker push image-registry/your-project-id/kstream-app:1.0
  ```
  ![image](screenshots/12%20docker%20push%20app.PNG)
- Update `kstream-app.yaml`
  ```
  #insert-your-image-registry-address-here --> bobriashovm2/kstream-app:1.4
  ```

- Run you KStream app container in the K8s kluster alongside with Kafka Connect. Don't forger to update [Kubernetes deployment](kstream-app.yaml)
  with valid registry for your image
  ```cmd
  $ kubectl create -f kstream-app.yaml
  ```
  ![image](screenshots/13%20kubectl%20create%20app.PNG)
- Check Confluence Center Consumers
  ![image](screenshots/14%20app%20on%20confluence%20center.PNG)
- Check `expedia-ext` for enriched data
  ![image](screenshots/15%20expedia%20ext%20topic.PNG)


## Visualize data
Notes:   
Use `auto.offset.reset` = `Earliest` whenever it's possible on UI and `SET 'auto.offset.reset'='earliest';` in case of ksqldb.  
For access to kslqdb CLI use `kubectl exec -it ksqldb-0 -- ksql`.  
Check `ksql/scripts` directory for `statements.sql` file.

- Create stream based on enriched data topic
  ![image](screenshots/16%20create%20stream.PNG)
- Create table with aggregated data based on this stream
  ![image](screenshots/17%20create%20table.PNG)
- Query result table:
  ![image](screenshots/18%20result.png)

### Don't forget to destroy your infrastructure after with:
  ```
  terraform destroy
  ```
  
  