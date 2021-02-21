all: main.jar

main.jar:
	javac -cp "src" "src/Main.java" -d "build"

run: main.jar
	java -Xmx1024M -Xms1024M -cp "build" Main