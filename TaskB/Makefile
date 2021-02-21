all: main.jar

main.jar:
	javac -cp "src" "src/Main.java" -d "build"

run: main.jar
	java -Xmx512M -Xms512M -cp "build" Main