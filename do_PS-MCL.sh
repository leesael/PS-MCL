cd $(dirname $(readlink -f $0))


./PS-MCL.sh dataset/BioGrid/homo/homo ./ -sc 3 1.5 -reg 4 1 0 0.7
./measure.sh homo_PS-MCL-CoarseLevel-3_numThread-4.assign dataset/reference/homo/homo homo_nodemap


 