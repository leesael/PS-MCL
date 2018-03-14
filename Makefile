all: install demo

demo:
	./do_PS-MCL.sh

install:
	chmod +x ./*.sh
	chmod +x PS-MCL R-MCL B-MCL MCL
