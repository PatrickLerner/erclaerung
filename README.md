# erclaͤrung™

<img src="https://raw.githubusercontent.com/PatrickLerner/erclaerung/master/etc/cool_logo.png" align="right" />

erclaͤrung™ was born out of a simple dream: To create the best analyzer and text classifier for Early Modern German texts in the world.

To achieve this lofty (yet noble) goal, we are primarily using the [dkpro-core](https://code.google.com/p/dkpro-core-asl/) and [dkpro-tc](https://code.google.com/p/dkpro-tc/) framework for Java.

The primary corpora which is used for this project is the [Bonner Frühneuhochdeutschkorpora](http://korpora.zim.uni-due.de/Fnhd/). It is primarily serves as an already properly tagged and annotated source which can be utilized to compare new texts against.

If you want to know more about the project, you can check out our awesome presentation on [Google Drive](https://docs.google.com/presentation/d/1IOXK5maZECvb5QZ8MMeb9yPWgfYAVyUJxAvEunpGy6M/edit?usp=sharing) (German only).

Note that due to the licensing terms of the Bonner corpora, it is *not* supplied with this project. You must download the XML files from their website and put them into the `src/main/resources/bonner_korpora` folder of this project. Please do not forget to copy the `Fnhd.dtd` file along with the XML-files.
