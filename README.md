El pryecto está formado por varias ramas. Las más importantes son master, develop y realise.

master - Es la rama madre de la que parten las demás y solo se utilizará para unir las versiones finalizadas del proyecto.

develop - Es la rama más utilizada y de ella salen ramas secundarias con el objetivo de cumplir una funcionalidad. Por ejemplo ComunicaciónJSON es una rama que se crea con el objetivo de cambiar el proyecto inicial para que las comunicaciones entre agentes se realice utilizando JSON. 

Estas ramas secundarias se unirán a develop una vez han cumplido su objetivo y eliminarán en el caso de no necesidad o de haber cumplido realmente el objetivo con la que se crearon.

realise - Se utilizará para testear versiones finales del proyecto una vez de ha terminado su desarrollo. Si cumplen con los requisitos que se piden y no hay ningún error se podrán unir a master.

Además de esto, se insta a utilizar descripciones de commit extensas y explicativas. En el caso de ocurrir conflictos es necesario poner las corracciones que se han realizado en la descripción del commit además de dejar constancia que ha habido un conflicto.