.DEFAULT_GOAL := package

src/sources.txt:
	(cd src; find -name "*.java" > sources.txt)

.PHONY: compile
compile: src/sources.txt
	mkdir -p build
	(cd src; javac -d ../build @sources.txt)

.PHONY: package
package: compile
	(cd build; jar cvf ../patu.jar *)
