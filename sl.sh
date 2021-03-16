#!/bin/bash
#SBATCH -J prep         
#SBATCH -p ttxp              
#SBATCH -N 1                 
#SBATCH --ntasks-per-node=4  
#SBATCH --cpus-per-task=1    
#SBATCH -t 1-20:00:00        

java -jar target/data-process-1.0.jar
