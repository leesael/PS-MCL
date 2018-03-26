all: install demo

demo:
	./do_PS-MCL.sh

install:
	chmod +x ./*.sh
	./do_compile_PS-MCL.sh
	chmod +x PS-MCL R-MCL B-MCL MCL
