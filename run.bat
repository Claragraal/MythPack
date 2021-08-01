rmdir /s output
mkdir output

cd "6_Myth1.16.2+"
tar.exe -a -c -f "./../output/6.zip" assets pack.png pack.mcmeta
cd ..

cd "5_Myth1.15+"
tar.exe -a -c -f ./../output/5.zip assets pack.png pack.mcmeta
cd ..

cd "4_Myth1.13+"
tar.exe -a -c -f ./../output/4.zip assets pack.png pack.mcmeta
cd ..

cd "3_Myth1.11+"
tar.exe -a -c -f ./../output/3.zip assets pack.png pack.mcmeta
cd ..

cd "2_Myth1.9+"
tar.exe -a -c -f ./../output/2.zip assets pack.png pack.mcmeta
cd ..

cd "1_Myth1.8+"
tar.exe -a -c -f ./../output/1.zip assets pack.png pack.mcmeta
cd ..

java -jar ResPacker/target/ResPacker-1.0-SNAPSHOT.jar
PAUSE