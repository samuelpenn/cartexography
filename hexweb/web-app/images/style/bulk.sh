#!/bin/sh

if [ ! -d $1.xcf ]
then
	echo "No such directory"
	exit 2
fi
cd $1.xcf/terrain

gimp -n -i -b - <<EOF
(let* ( (file's (cadr (file-glob "*.xcf" 1))) (filename "") (image 0) (layer 0) )
  (while (pair? file's) 
    (set! image (car (gimp-file-load RUN-NONINTERACTIVE (car file's) (car file's))))
    (set! layer (car (gimp-image-merge-visible-layers image CLIP-TO-IMAGE)))
    (set! filename (string-append (substring (car file's) 0 (- (string-length (car file's)) 4)) ".png"))
    (gimp-file-save RUN-NONINTERACTIVE image layer filename filename)
    (gimp-image-delete image)
    (set! file's (cdr file's))
    )
  (gimp-quit 0)
  )
EOF

cd ../..
mkdir -p $1/terrain
mv $1.xcf/terrain/*png $1/terrain
