export const zip = <A, B>(listA: A[], listB: B[]): (A | B)[][] => {
  return listA.map((item, index) => [item, listB[index]]);
};

export const distinct = <T>(result: T[], current: T): T[] => {
  if (result.length === 0) {
    return [current];
  }
  if (result.includes(current)) {
    return result;
  }

  return result.concat(current);
};
