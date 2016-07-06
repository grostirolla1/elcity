# Begin of the log file: 	2014-02-01 00:00:00
# End of the log file:		2014-03-02 23:59:59

#GO to correct dir
data.dir <- "/Users/grostirolla/Dropbox/Dissertação/logs/Points"
run.dir <- "05-20-00-11-20" 
dir <- paste(data.dir,run.dir, sep="/")
out.dir <- "/Users/grostirolla/Dropbox/Dissertação/logs/Results/Points/"
setwd(dir)

#Set Constant values for calculus
onInterval <- 5*60 #In Seconds
hourOnBegin <- c(as.POSIXct("19:00:00", format="%H:%M:%S",tz = ""))			 #7 PM Current Day
hourOnEnd <- c(as.POSIXct("06:00:00", format="%H:%M:%S", tz = "",))+60*60*24 #6 AM Next Day
beginLog <- c(as.POSIXct("2014-02-01 00:00:00", tz = ""))
endLog <- c(as.POSIXct("2014-03-02 23:59:59", tz = ""))	
#data from http://www.geimprensabrasil.com/iluminacao-publica-eficiente-torna-cidades-brasileiras-mais-sustentaveis
#1 = Sodium vapor lamp - 2 = LED lamp
device1Consumption <- 150 #in Watts
device2Consumption <- 90 #in Watts 

# All point files have the extension .log
logRegExp = "(.+).log$"
logFiles <- list.files (path = ".", pattern = logRegExp, full.names = TRUE)

#Function that analyses all logs
analyseFile <- function(currentFile){

	df <- read.csv(currentFile, header=TRUE, sep=",")
	# CUT OUT ALL THE VALUES THAT ARE NO IN THE HOURON RANGE (DAY TIME)
	df$TimeTmpCal <-as.POSIXct(df$Time, format="%H:%M",tz = "")
	df <- df[(df$TimeTmpCal > hourOnBegin & df$TimeTmpCal< hourOnEnd),]
	colsToDrop <- c("TimeTmpCal")
	df <- df[,!names(df) %in% colsToDrop]

	final<- NULL
	if(nrow(df)>0){
		if(nrow(df)>=2){
			df$Timestamp <- paste(df[,"Day"],df[,"Time"],sep=" ")
			df$Timestamp <- c(as.POSIXct(df[,"Timestamp"], tz = ""))

			#CALCULATES THE OFF TIME OF ALL LAMPS
			# subtract the time considering the difference where the lamp would be on ( + and - onInterval)
		    df$off <- c(0,difftime((df[2:nrow(df),]$Timestamp - onInterval), (df[1:nrow(df)-1,]$Timestamp + onInterval), units="secs"))
		    # Fix/add the first and last lines with the begin and end of the log file captures
		    df$off[1] <- c(difftime((df[1,]$Timestamp - onInterval), beginLog,units="secs"))
		    df <- rbind(df,data.frame(Day=0,Time=0,Users=0,Timestamp=as.POSIXct(endLog, origin="1970-01-01"),off=0))
		    df$off[nrow(df)] <- c(difftime(endLog, (df[nrow(df)-1,]$Timestamp + onInterval), units="secs"))

			# Change negative numbers to 0 (not enough time to turn off)
		    df$off[df$off<0]<-0

			#CALCULATES THE ON/OFF TIME OF ALL LAMPS WITHOUT INTELLIGENCE
			#Gives the time that lamps would be on by day without intelligence in seconds
			secsOnWI <- difftime(hourOnEnd,hourOnBegin, units="secs") 
			#Days that the log contains
			daysLog <- as.numeric(difftime(endLog, beginLog, units="days"))
			#Total time that the lamps would be on without inteligence
			totalTimeOnSecsWI <- secsOnWI*daysLog
		    totalTimeOffSecsWI <- as.numeric(difftime(endLog, beginLog, units="secs")) - totalTimeOnSecsWI

			#CALCULATES THE ON/OFF TIME OF ALL LAMPS WITH INTELLIGENCE
		    #Total Time That The Lamps Would Be Off in Smart mode 
		    timeOffSecs <- sum(df$off)
		    timeOnSecs <-  (totalTimeOnSecsWI+totalTimeOffSecsWI) - timeOffSecs

		    #mount the return data frame
    		point <- sub("*./", "",sub("*.log", "", currentFile))
    		final["Point"]<-point
			final["totalTimeOnSecsWI"]<-totalTimeOnSecsWI
			final["totalTimeOffSecsWI"]<-totalTimeOffSecsWI
			final["timeOnSecs"]<-timeOnSecs
			final["timeOffSecs"]<-timeOffSecs

	    }
	    if(nrow(df)==1){
	    	df$Timestamp <- paste(df[,"Day"],df[,"Time"],sep=" ")
			df$Timestamp <- c(as.POSIXct(df[,"Timestamp"], tz = ""))
	    	df$off <- c(difftime((df[1,]$Timestamp - onInterval), beginLog,units="secs"))
	    	df <- rbind(df,data.frame(Day=0,Time=0,Users=0,Timestamp=as.POSIXct(endLog, origin="1970-01-01"),off=0))
		    df$off[nrow(df)] <- c(difftime(endLog, (df[nrow(df)-1,]$Timestamp + onInterval), units="secs"))

	    	# Change negative numbers to 0 (not enough time to turn off)
		    df$off[df$off<0]<-0

			#CALCULATES THE ON/OFF TIME OF ALL LAMPS WITHOUT INTELLIGENCE
			#Gives the time that lamps would be on by day without intelligence in seconds
			secsOnWI <- difftime(hourOnEnd,hourOnBegin, units="secs") 
			#Days that the log contains
			daysLog <- as.numeric(difftime(endLog, beginLog, units="days"))
			#Total time that the lamps would be on without inteligence
			totalTimeOnSecsWI <- secsOnWI*daysLog
		    totalTimeOffSecsWI <- as.numeric(difftime(endLog, beginLog, units="secs")) - totalTimeOnSecsWI

			#CALCULATES THE ON/OFF TIME OF ALL LAMPS WITH INTELLIGENCE
		    #Total Time That The Lamps Would Be Off in Smart mode 
		    timeOffSecs <- sum(df$off)
		    timeOnSecs <-  (totalTimeOnSecsWI+totalTimeOffSecsWI) - timeOffSecs
		    
		    #mount the return data frame
    		point <- sub("*./", "",sub("*.log", "", currentFile))
    		final["Point"]<-point
			final["totalTimeOnSecsWI"]<-totalTimeOnSecsWI
			final["totalTimeOffSecsWI"]<-totalTimeOffSecsWI
			final["timeOnSecs"]<-timeOnSecs
			final["timeOffSecs"]<-timeOffSecs

	    }

    }
    else{
    	#All Time Off

		#CALCULATES THE ON/OFF TIME OF ALL LAMPS WITHOUT INTELLIGENCE
		#Gives the time that lamps would be on by day without intelligence in seconds
		secsOnWI <- difftime(hourOnEnd,hourOnBegin, units="secs") 
		#Days that the log contains
		daysLog <- as.numeric(difftime(endLog, beginLog, units="days"))
		#Total time that the lamps would be on without inteligence
		totalTimeOnSecsWI <- secsOnWI*daysLog
	    totalTimeOffSecsWI <- as.numeric(difftime(endLog, beginLog, units="secs")) - totalTimeOnSecsWI
    	timeOffSecs <- (secsOnWI*daysLog)+totalTimeOffSecsWI
    	timeOnSecs <- 0
    	point <- sub("*./", "",sub("*.log", "", currentFile))
		final["Point"]<-point
		final["totalTimeOnSecsWI"]<-totalTimeOnSecsWI
		final["totalTimeOffSecsWI"]<-totalTimeOffSecsWI
		final["timeOnSecs"]<-timeOnSecs
		final["timeOffSecs"]<-timeOffSecs
    }

    final
}

#Analyse logs
	allFrames <- NULL
    allFrames <- lapply(logFiles,function(x) analyseFile(x))

	dfresults <- as.data.frame(do.call(rbind,allFrames))

	dfresults$joulesDev1OnWI <- as.numeric(as.character(dfresults$totalTimeOnSecsWI)) * device1Consumption
	dfresults$joulesDev1OffWI <- as.numeric(as.character(dfresults$totalTimeOffSecsWI)) * device1Consumption
	dfresults$joulesDev1OnI <- as.numeric(as.character(dfresults$timeOnSecs)) * device1Consumption
	dfresults$joulesDev1OffI <-as.numeric(as.character(dfresults$timeOffSecs)) * device1Consumption

	dfresults$joulesDev2OnWI <- as.numeric(as.character(dfresults$totalTimeOnSecsWI)) * device2Consumption
	dfresults$joulesDev2OffWI <- as.numeric(as.character(dfresults$totalTimeOffSecsWI)) * device2Consumption
	dfresults$joulesDev2OnI <- as.numeric(as.character(dfresults$timeOnSecs)) * device2Consumption
	dfresults$joulesDev2OffI <-as.numeric(as.character(dfresults$timeOffSecs)) * device2Consumption

	sums <- NULL
	sums$kjoulesDev1OnWI <- sum(dfresults$joulesDev1OnWI)/1000
	sums$kjoulesDev1OffWI <- sum(dfresults$joulesDev1OffWI)/1000
	sums$kjoulesDev1OnI <- sum(dfresults$joulesDev1OnI)/1000
	sums$kjoulesDev1OffI <- sum(dfresults$joulesDev1OffI)/1000
	sums$kjoulesDev2OnWI <- sum(dfresults$joulesDev2OnWI)/1000
	sums$kjoulesDev2OffWI <- sum(dfresults$joulesDev2OffWI)/1000
	sums$kjoulesDev2OnI <- sum(dfresults$joulesDev2OnI)/1000
	sums$kjoulesDev2OffI <- sum(dfresults$joulesDev2OffI)/1000

	sums

	write.csv(dfresults, file.path(out.dir, "dfpoints.csv"))
	write.csv(sums, file.path(out.dir, "sums.csv"))
