all: main.jar

main.jar:
	javac -cp "src" "src/Main.java" -d "build"

run: main.jar
	java -Xmx500M -Xms500M -cp "build" Main