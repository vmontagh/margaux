
output=Merged_$(date +%Y-%m-%d-%H-%M-%S).txt
##Header is common among all files

files=`find . -maxdepth 1 -type f -name '*.out.txt'`
files_arr=($files)

header=`head -n 1 "${files_arr[0]}"`
echo $header > "$output"

for file in $files; do
	sed "/$header/d" "$file" >> "$output"
done

sed -i 's/_IMPLY_/,imply,/' "$output"
sed -i 's/_IMPLY_/,and,/' "$output"
