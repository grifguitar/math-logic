all: main.jar

main.jar:
	javac -cp "src" "src/TaskB/Main.java"

run: main.jar
	java -Xmx500M -Xms500M -cp "src" TaskB.Main