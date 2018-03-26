cd $(dirname $(readlink -f $0))

echo compiling java sources...
rm -rf class
mkdir class
javac -d class $(find ./src -name *.java)

echo make jar archive... 
cd class
echo Main-Class: mcl.MCL > manifest.mf
jar cmvf manifest.mf PS-MCL.jar ./
rm ../bin/PS-MCL.jar
mv PS-MCL.jar ../bin/ 
cd ..
rm -rf class

echo done.
