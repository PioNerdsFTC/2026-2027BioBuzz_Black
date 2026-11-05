const mgRatio = 1;
const oRatio = 1.0635625;

for (let i = 0; i < 20000; i++) {
    const oxygenRatio = oRatio * i;
    const wholeNumber = Math.trunc(oxygenRatio);

    // if (
    //     wholeNumber > oxygenRatio - 0.0001 &&
    //     wholeNumber < oxygenRatio + 0.0001
    // ) {
    if (wholeNumber === oxygenRatio) {
        console.log("Found candidate", mgRatio * i, oxygenRatio);
    }
}

