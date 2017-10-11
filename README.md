El proyecto está formado por varias ramas. Las más importantes son master, develop y realise.

master - Es la rama madre de la que parten las dem�s y solo se utilizar� para unir las versiones finalizadas del proyecto.

develop - Es la rama m�s utilizada y de ella salen ramas secundarias con el objetivo de cumplir una funcionalidad. Por ejemplo ComunicaciónJSON es una rama que se crea con el objetivo de cambiar el proyecto inicial para que las comunicaciones entre agentes se realice utilizando JSON. 

Estas ramas secundarias se unirán a develop una vez han cumplido su objetivo y eliminarán en el caso de no necesidad o de haber cumplido realmente el objetivo con la que se crearon.

Además de esto, se insta a utilizar descripciones de commit extensas y explicativas. En el caso de ocurrir conflictos, es beneficioso poner las correcciones que se han realizado en la descripción del commit además de dejar constancia que ha habido un conflicto.

DEPENDENCIAS

Las principales dependencias del proyecto son JADE (http://jade.tilab.com/download/jade/). Para poder ejecutar el proyecto es necesario añadir las librerias de ambas dependencias.