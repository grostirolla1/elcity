library(plyr)
library(ggplot2)
library(reshape)
library (stringr)
library(prospectr)

carga<-"30-70-316U-50P"
# carga<-"30-70"
arq<-paste(carga, sep="")
data.dir <- "/Users/grostirolla/Dropbox/Dissertação/logs/25-50P/Nodes"
# data.dir <- "/Users/grostirolla/Dropbox/Dissertação/logs/Nodes"
filename<-paste(arq,".csv", sep="")

out.dir <- paste("/Users/grostirolla/Dropbox/Dissertação/logs/25-50P/Results/",arq,sep="")
dir.create(out.dir, showWarnings = TRUE, recursive = TRUE, mode = "0777")


filenameDados<-paste("Dados",filename, sep="-")
filenamePower<-paste("Power",filename, sep="-")
filenameAlloc<-paste("AllocTime",filename, sep="-")
filenameFinal<-paste("Final",filename, sep="-")

############ CARREGA DADOS DE TREINO ####################
    dfmonit<-read.csv2(file="~/Dropbox/Artigos/EnergyCloud/testes/Consumo/cpu-mem.log",header=TRUE)
    dfpower<-read.csv(file="~/Dropbox/Artigos/EnergyCloud/testes/Consumo/power.csv",header=FALSE)

############ CARREGA THRESHOLD A SER ANALISADO ####################
	allocTime <- read.csv2(file.path(data.dir, filename),header=TRUE)

############ TRATAMENTO DOS DADOS DE TREINO ####################

    dfmonit$Time <- c(as.POSIXct(strptime(dfmonit[,"Time"],"%H:%M:%S")))
    dfmonit$DeltaTM <- c(0,dfmonit[2:nrow(dfmonit),]$Time -  dfmonit[1:nrow(dfmonit)-1,]$Time)

    ############ REDE ####################
    dfmonit["TX B/s"] <-  c(0,(dfmonit[2:nrow(dfmonit),]$NETBytesTX -  dfmonit[1:nrow(dfmonit)-1,]$NETBytesTX)/dfmonit[2:nrow(dfmonit),]$DeltaT)
    dfmonit["RX B/s"] <-  c(0,(dfmonit[2:nrow(dfmonit),]$NETBytesRX -  dfmonit[1:nrow(dfmonit)-1,]$NETBytesRX)/dfmonit[2:nrow(dfmonit),]$DeltaT)
    #dfmonit$TXPercent <- dfmonit["TX B/s"]/max(dfmonit["TX B/s"])
    #dfmonit$RXPercent <- dfmonit["RX B/s"]/max(dfmonit["RX B/s"])

	############ MEMÓRIA ####################    
    #USED MEMORY KB
    #MemTotal - MemFree - Buffers - Cached
    dfmonit["UsedMem KB"] <-  c(dfmonit$MEMTotal-dfmonit$MEMFree-dfmonit$MEMBuffers-dfmonit$MEMCached)
    #FREE MEMORY KB
    #MemFree + Buffers + Cached
    dfmonit["FreeMem KB"] <-  c(dfmonit$MEMFree+dfmonit$MEMBuffers+dfmonit$MEMCached)
    dfmonit["UsedMem %"] <- c(dfmonit["UsedMem KB"]/(dfmonit["UsedMem KB"]+dfmonit["FreeMem KB"])*100)

  	############ CPU ####################   
    #http://phoxis.org/2013/09/05/finding-overall-and-per-core-cpu-utilization/
    #https://github.com/hishamhm/htop/blob/master/ProcessList.com
    #https://gist.github.com/creaktive/781248
    #Idle=idle+iowait
    #NonIdle=user+nice+system+irq+softirq+steal
    #Total=Idle+NonIdle
    #CPU_Percentage=((Total-PrevTotal)-(Idle-PrevIdle))/(Total-PrevTotal)
    dfmonit["CPUIdleTot"] <-  c(dfmonit$CPUIdle+dfmonit$CPUIOWait)
    dfmonit["CPUNonIdleTot"] <-  c(dfmonit$CPUUser+dfmonit$CPUNice+dfmonit$CPUSystem+dfmonit$CPUIrq+dfmonit$CPUSoftIrq+dfmonit$CPUSteal)
    dfmonit["CPUTotal"] <-  c(dfmonit$CPUIdleTot+dfmonit$CPUNonIdleTot)
    dfmonit["CPUUsage %"] <-  c(0,100*((dfmonit[2:nrow(dfmonit),]$CPUTotal -  dfmonit[1:nrow(dfmonit)-1,]$CPUTotal)-(dfmonit[2:nrow(dfmonit),]$CPUIdleTot -  dfmonit[1:nrow(dfmonit)-1,]$CPUIdleTot))/(dfmonit[2:nrow(dfmonit),]$CPUTotal -  dfmonit[1:nrow(dfmonit)-1,]$CPUTotal))
   
    ############ POWER #################### 
    dfpower<-subset(dfpower, select = -c(V1))
    dfpower<-rename(dfpower, c("V2"="Time", "V3"="Power", "V4"="Factor"))
    dfpower$Time <- c(as.POSIXct(strptime(dfpower[,"Time"],"%H:%M:%S")))
    dfpower$DeltaTP <- c(0,dfpower[2:nrow(dfpower),]$Time -  dfpower[1:nrow(dfpower)-1,]$Time)

    ############ MERGE 2 DATAFRAMES #################### 
    dffinalScript<-merge(dfpower,dfmonit,by="Time")

	dataScript<- data.frame(dffinalScript$"CPUUsage %",dffinalScript$"UsedMem %",dffinalScript$Power)
	names(dataScript) <- c("CPU", "Memory","Power")

	############ ANAĹISE PCA E KAISER #################### 
	#http://little-book-of-r-for-multivariate-analysis.readthedocs.org/en/latest/src/multivariateanalysis.html
	standardisedconcentrations <- as.data.frame(scale(dataScript[1:2]))
	pcaScript <-prcomp(standardisedconcentrations)

	#Kaiser's
	(pcaScript$sdev)^2

	trainingScript <- dataScript


############ TRATAMENTO DOS DADOS DE LOG ####################

## extensão .log são os arquivos contendo informação de consumo
logRegExp = "(.+).log$"
## encontra todos os arquivos segundo a expressão regular
logFiles <- list.files (path = ".", pattern = logRegExp, full.names = TRUE)

allocTime$AllocationTime <- c(as.POSIXct(allocTime[,"AllocationTime"],origin="1970-01-01"))
allocTime$DeallocationTime <- c(as.POSIXct(allocTime[,"DeallocationTime"],origin="1970-01-01"))

############ FUNÇÕES DE ANÁLISE DOS LOGS #############################

########################################################################
## Recebe um dataframe e os tempos de alocação e desalocação 
## de um nó e constroi um dataframe (auxiliar da função analyseFile)
##
## @param df o dataframe com as amostras de um nó específico
## @param allocDealloc dataframe contendo o inicio e o fim do uso do nó
##
## @return dataframe contendo os dados neste intervalo de tempo
########################################################################
dfDuration <- NULL
splitLog <- function(df, allocDealloc) {
	ini <- as.POSIXct(strptime(allocDealloc[2],"%Y-%m-%d %H:%M:%S"))
	fim <- as.POSIXct(strptime(allocDealloc[3],"%Y-%m-%d %H:%M:%S"))

	# print("INI")
	# print(ini)
	# print(str(ini))
	# print("FIM")
	# print(fim)
	# print(str(fim))
	
	dfSplit <- df[df$Time >= ini & df$Time <= fim, ]

	dfSplit
}
########################################################################
## Recebe o nome de um arquivo de log, processa o arquivo e retorna
## um novo dataframe contendo apenas o tempo em que a máquina
## estava sendo utilizada (alocada)
##
## @param currentFile arquivo de log com o nome do nó
##
## @return dataframe contendo os as leituras neste intervalo de tempo
########################################################################
analyseFile <- function(currentFile) {

    ## leitura do primeiro log
    df <- read.csv2(currentFile, header=TRUE, sep=";")

    ## calcula a duração entre cada período df[1..n]$Time - df[0..n-1]$Time
    ## assim pode calcular a integral e o consumo efetivo
    ############ TEMPO ####################
    df$Time <- c(as.POSIXct(df[,"TimeEpoch"],origin="1970-01-01"))
    df$Duration <- c(0,df[2:nrow(df),]$Time -  df[1:nrow(df)-1,]$Time)

	############ MEMÓRIA ####################    
    df["UsedMem KB"] <-  c(df$MEMTotal-df$MEMFree-df$MEMBuffers-df$MEMCached)
    df["FreeMem KB"] <-  c(df$MEMFree+df$MEMBuffers+df$MEMCached)
    df["UsedMem %"] <- c(df["UsedMem KB"]/(df["UsedMem KB"]+df["FreeMem KB"])*100)
  	############ CPU ####################   
    df["CPUIdleTot"] <-  c(df$CPUIdle+df$CPUIOWait)
    df["CPUNonIdleTot"] <-  c(df$CPUUser+df$CPUNice+df$CPUSystem+df$CPUIrq+df$CPUSoftIrq+df$CPUSteal)
    df["CPUTotal"] <-  c(df$CPUIdleTot+df$CPUNonIdleTot)
    df["CPUUsage %"] <-  c(0,100*((df[2:nrow(df),]$CPUTotal -  df[1:nrow(df)-1,]$CPUTotal)-(df[2:nrow(df),]$CPUIdleTot -  df[1:nrow(df)-1,]$CPUIdleTot))/(df[2:nrow(df),]$CPUTotal -  df[1:nrow(df)-1,]$CPUTotal))

  	############ SPLIT ALOCAÇÃO E DESALOCAÇÃO ####################   
    filename <- strsplit(currentFile,".log")[[1]][1]
    filename <- strsplit(filename,"/")[[1]][length(strsplit(filename,"/")[[1]])]

    #retorna todas as vezes que o nó foi alocado e desalocado
    allocs <- as.data.frame(allocTime[which(allocTime$Host == filename), ])

    #combina o retorno das chamadas de splitLog (todas as vezes que o nó foi alocado e desalocado)
    dfWithMeasures <- NULL
    dfWithMeasures <- do.call(rbind, apply(allocs,1,function(x) splitLog(df, x)))
    dfWithMeasures["Host"] <- filename

    dfWithMeasures
}


############ CHAMADAS DAS FUNÇÕES PARA ANÁLISE DOS LOGS #############################

    allFrames <- lapply(logFiles,function(x) analyseFile(x))

    removeNull <- c()
    for(i in 1:length(allFrames)){
    	if(is.null(nrow(allFrames[[i]])))
    		removeNull <- c(removeNull,i)
    }
    allFrames <- allFrames[-removeNull]

	dflogsalloc <- do.call(rbind,allFrames)

	dataColeta<- data.frame(dflogsalloc$Host, dflogsalloc$Time, dflogsalloc$Duration, dflogsalloc$"CPUUsage %",dflogsalloc$"UsedMem %")
	names(dataColeta) <- c("Host","Time","Duration","CPU", "Memory")

	write.csv(dataColeta, file.path(out.dir, filenameDados))

	#subset necessário para alguns dos previsores
	subsetColeta <- data.frame(dflogsalloc$"CPUUsage %",dflogsalloc$"UsedMem %")
	names(subsetColeta) <- c("CPU", "Memory")


############ REGRESSÕES LINEARES ####################


####################################
## PCR
####################################

library(pls)

mScript.pcr <- pcr(Power ~ .,data=trainingScript , validation="CV")

# select number of components (by CV)
ncompScript <- which.min(mScript.pcr$validation$adj)

# predict
y.predict.pcr <- predict(mScript.pcr,subsetColeta , ncomp=ncompScript)
dataColeta$PCR <- y.predict.pcr 

####################################
## Lasso
####################################
library(lars)


mScript.lasso <- lars(as.matrix(trainingScript[,1:2]),trainingScript[,3])

# Cross-validation
rScript <- cv.lars(as.matrix(trainingScript[,1:2]),trainingScript[,3])
bestfractionScript <- rScript$index[which.min(rScript$cv)]

# Observe coefficients
coefScript.lasso <- predict(mScript.lasso,as.matrix(subsetColeta),s=bestfractionScript,type="coefficient",mode="fraction")
coefScript.lasso

# Prediction
y.predScript.lasso <- predict.lars(mScript.lasso,as.matrix(subsetColeta),type="fit",mode="fraction")$fit

y.predScript.lassomean <- rowMeans(y.predScript.lasso)

dataColeta$LASSO <- y.predScript.lassomean


####################################
## ElasticNet
####################################
library(elasticnet)
elasticScript <- enet(as.matrix(trainingScript[,1:2]),as.vector(trainingScript[,3]),lambda=0.5)
#plot(elasticScript, use.color=TRUE)
cvelasticScript <- cv.enet(as.matrix(trainingScript[,1:2]),as.vector(trainingScript[,3]),lambda=0.5, s=seq(0,1,length=100), mode="fraction",trace=FALSE,max.steps=80)
y.predScript.elasticnet<-predict.enet(elasticScript,subsetColeta,type="fit",mode="step")
y.predScript.elasticmean <- rowMeans(y.predScript.elasticnet$fit)
dataColeta$ElasticNet <- y.predScript.elasticmean


############ REDES BAYESIANAS ####################

####################################
## Naive Bayes Network
####################################
#http://www.bnlearn.com/documentation/man/naive.bayes.html

#library(bnlearn)

#subsetColeta$Power <- 0

#resScript <- hc(trainingScript)
#fittedScript <- bn.fit(resScript,trainingScript)
#y.predScript.bnlearn <- predict(fittedScript$Power, subsetColeta)

#dataColeta$NaiveBayes <-y.predScript.bnlearn

#JOULES CALCULUS
dataColeta$JoulesPCR <- dataColeta$PCR * dataColeta$Duration
dataColeta$JoulesLASSO <- dataColeta$LASSO * dataColeta$Duration
dataColeta$JoulesElasticNet <- dataColeta$ElasticNet * dataColeta$Duration


#Write Predictions

write.csv(dataColeta, file.path(out.dir, filenamePower))


#allocTime$UsageTime <- allocTime$DeallocationTime - allocTime$AllocationTime
allocTime$UsageTime <- difftime(allocTime$DeallocationTime,allocTime$AllocationTime,unit="secs")
#write.csv2(allocTime, file="/home/grostirolla/Dropbox/Artigos/EnergyCloud/testes/Results/AllocTime-Onda50-90.csv")

max(dataColeta$Time)-min(dataColeta$Time)
sum(dataColeta$JoulesPCR)/1000
sum(dataColeta$JoulesLASSO)/1000
sum(dataColeta$JoulesElasticNet)/1000


#Soma do consumo nos diversos nós pelo tempo
dfsumm <- ddply(dataColeta,~Host,summarise,meanPCR=mean(PCR),sdPCR=sd(PCR),sumJoulesPCR=sum(JoulesPCR),meanLASSO=mean(LASSO),sdLASSO=sd(LASSO),sumJoulesLASSO=sum(JoulesLASSO),meanElasticNet=mean(ElasticNet),sdElasticNet=sd(ElasticNet),sumJoulesElasticNet=sum(ElasticNet))
dfsumm <- merge(allocTime,dfsumm,by="Host")

write.csv(dfsumm, file.path(out.dir, filenameAlloc))

dffinal <- dataColeta
dffinal$sFac <- droplevels(cut(dffinal$Time, breaks="3 sec"))
res1 <- aggregate(cbind(CPU, PCR, LASSO, ElasticNet) ~ sFac + Host, data=dffinal, FUN=mean)
dffinal<-ddply(res1,~sFac,summarise,sumCPU=sum(CPU),sumPCR=sum(PCR),sumLasso=sum(LASSO),sumElasticNet=sum(ElasticNet))


dffinal$sumPCRFilt<-c(dffinal$sumPCR[0:1],movav(dffinal$sumPCR,w=3),dffinal$sumPCR[(length(dffinal$sumPCR)):(length(dffinal$sumPCR))])
dffinal$sumLassoFilt<-c(dffinal$sumLasso[0:1],movav(dffinal$sumLasso,w=3),dffinal$sumLasso[(length(dffinal$sumLasso)):(length(dffinal$sumLasso))])
dffinal$sumElasticNetFilt<-c(dffinal$sumElasticNet[0:1],movav(dffinal$sumElasticNet,w=3),dffinal$sumElasticNet[(length(dffinal$sumElasticNet)):(length(dffinal$sumElasticNet))])

write.csv(dffinal, file.path(out.dir, filenameFinal))


#dffinal$sumPCRFilt3<-c(dffinal$sumPCR[0:1],movav(dffinal$sumPCR,w=3),dffinal$sumPCR[(length(dffinal$sumPCR)):(length(dffinal$sumPCR))])
#dffinal$sumPCRFilt5<-c(dffinal$sumPCR[0:2],movav(dffinal$sumPCR,w=5),dffinal$sumPCR[(length(dffinal$sumPCR)-1):(length(dffinal$sumPCR))])
#dffinal$sumPCRFilt7<-c(dffinal$sumPCR[0:3],movav(dffinal$sumPCR,w=7),dffinal$sumPCR[(length(dffinal$sumPCR)-2):(length(dffinal$sumPCR))])
#dffinal$sumPCRFilt9<-c(dffinal$sumPCR[0:4],movav(dffinal$sumPCR,w=9),dffinal$sumPCR[(length(dffinal$sumPCR)-3):(length(dffinal$sumPCR))])
#dffinal$sumPCRFilt15<-c(dffinal$sumPCR[0:7],movav(dffinal$sumPCR,w=15),dffinal$sumPCR[(length(dffinal$sumPCR)-6):(length(dffinal$sumPCR))])
