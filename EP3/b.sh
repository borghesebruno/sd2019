#!/bin/bash

javac ClientRequest.java MapperResponse.java ReducerResponse.java

javac -cp .:jsoup-1.12.1.jar Client.java Coordenador.java Mapper.java Reducer.java