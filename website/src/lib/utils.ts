function dotProduct(vectorA: number[], vectorB: number[]) {
	if (vectorA.length !== vectorB.length) {
		throw new Error('Vectors must have the same length');
	}

	let dotProduct = 0;
	for (let i = 0; i < vectorA.length; i++) {
		dotProduct += vectorA[i] * vectorB[i];
	}

	return dotProduct;
}

function summarizeList(list: number[]) {
	if (list.length <= 10) {
		return `[${list.join(',')}]`;
	}

	const start = list.slice(0, 3).map((num) => num.toFixed(4));
	const end = list.slice(-3).map((num) => num.toFixed(4));

	return `[${start.join(',')},...,${end.join(',')}]`;
}

export { dotProduct, summarizeList };
