# Instruction to compile 
===========================
1. Open the command line or terminal 
2. javac <file that has been modified> e.g. javac MyCharServer.java



# Instruction to run 
===========================
1. Navigate to the src folder
2. Open command lines/ terminals
3. Run the programs in the order:
  1. java MyCharFreqSever <PortNumber> <NumWorker>
  2. java MyCharFreqWorker <ServerIP> <PortNumber>
  3. java MyCharFreqClient <ServerIP> <PortNumber>

  * Note:
    1. SeverIP has to been localhost (127.0.0.1)
    2. PortNumber of MyCharFreqWorker and MyCharFreqClient have to match with  MyCharFreqServer


