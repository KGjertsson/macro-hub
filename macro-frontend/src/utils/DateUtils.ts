export const dateToString = (d: Date) => {
  // Extract year, month, and day components
  const year = d.getFullYear(); // Get the 4-digit year
  const month = String(d.getMonth() + 1).padStart(2, '0'); // Get the month (add 1 because months are zero-based) and pad with '0' if necessary
  const day = String(d.getDate()).padStart(2, '0'); // Get the day of the month and pad with '0' if necessary

  // Create the formatted string
  return `${year}-${month}-${day}`;
};
