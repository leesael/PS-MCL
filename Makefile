all: install demo

demo:
	./do_PS-MCL.sh

install:
	chmod +x ./*.sh
	./compile_PS-MCL.sh
